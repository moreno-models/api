import * as cdk from "aws-cdk-lib";
import { Aspects } from "aws-cdk-lib";
import { Construct } from "constructs";
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as rds from 'aws-cdk-lib/aws-rds';
import * as iam from 'aws-cdk-lib/aws-iam';
import { CfnDBCluster } from 'aws-cdk-lib/aws-rds';


export class StorageStack extends cdk.Stack {
    public readonly dbCluster: rds.DatabaseCluster;
    public readonly vpc: ec2.Vpc;

    constructor(scope: Construct, id: string, props?: cdk.StackProps) {
        super(scope, id, props);

        const vpc = new ec2.Vpc(this, 'Vpc', {
            cidr: '10.0.0.0/16',
            subnetConfiguration: [
                { name: 'public', subnetType: ec2.SubnetType.PUBLIC },
                { name: 'internal', subnetType: ec2.SubnetType.PRIVATE_ISOLATED }
            ],
            natGateways: 0,
        })
        this.vpc = vpc;

        const dbSecurityGroup = new ec2.SecurityGroup(this, 'DbSecurityGroup', {
            vpc: vpc,
            allowAllOutbound: true,
        })

        dbSecurityGroup.addIngressRule(
            ec2.Peer.anyIpv4(),
            ec2.Port.tcp(5432),
            'Allow everyone in the VPC to read / write to aurora'
        );

        // Full spec https://github.com/aws/aws-cdk/issues/20197#issuecomment-1117555047
        this.dbCluster = new rds.DatabaseCluster(this, 'DbCluster', {
            engine: rds.DatabaseClusterEngine.auroraPostgres({
                version: rds.AuroraPostgresEngineVersion.VER_13_6,
            }),
            instances: 1,
            instanceProps: {
                vpc: vpc,
                instanceType: new ec2.InstanceType('serverless'),
                autoMinorVersionUpgrade: true,
                publiclyAccessible: true,
                securityGroups: [dbSecurityGroup],
                vpcSubnets: vpc.selectSubnets({
                    subnetType: ec2.SubnetType.PRIVATE_ISOLATED,
                }),
            },
            defaultDatabaseName: 'morenomodels',
            clusterIdentifier: 'morenomodels',
            port: 5432,
        });
        const s3Endpoint = vpc.addGatewayEndpoint('S3 Endpoint', {
            service: ec2.GatewayVpcEndpointAwsService.S3
        });
        s3Endpoint.addToPolicy(
            new iam.PolicyStatement({
                principals: [new iam.AnyPrincipal()],
                actions: ['s3:*'],
                resources: ['*'],
            }),
        );
        const smEndpoint = vpc.addInterfaceEndpoint('Secrets Manager', {
            service: ec2.InterfaceVpcEndpointAwsService.SECRETS_MANAGER,
            privateDnsEnabled: true,
            subnets: vpc.selectSubnets({
                subnetType: ec2.SubnetType.PRIVATE_ISOLATED,
            }),
        });
        smEndpoint.addToPolicy(
            new iam.PolicyStatement({
                principals: [new iam.AnyPrincipal()],
                actions: ['secretsmanager:*'],
                resources: ['*'],
            }),
        );

        Aspects.of(this.dbCluster).add({
            visit(node) {
                if (node instanceof CfnDBCluster) {
                    node.serverlessV2ScalingConfiguration = {
                        minCapacity: 0.5,
                        maxCapacity: 16,
                    }
                }
            },
        });
    }
}