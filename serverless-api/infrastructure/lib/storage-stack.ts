import * as cdk from "aws-cdk-lib";
import {Aspects, Duration} from "aws-cdk-lib";
import {Construct} from "constructs";
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as rds from 'aws-cdk-lib/aws-rds';
import {CfnDBCluster} from "aws-cdk-lib/aws-rds";


export class StorageStack extends cdk.Stack {
    constructor(scope: Construct, id: string, props?: cdk.StackProps) {
        super(scope, id, props);

        const vpc = new ec2.Vpc(this, 'Vpc', {
            cidr: '10.0.0.0/16',
            subnetConfiguration: [{ name: 'egress', subnetType: ec2.SubnetType.PUBLIC }],
            natGateways: 0,
        })

        const dbSecurityGroup = new ec2.SecurityGroup(this, 'DbSecurityGroup', {
            vpc: vpc,
            allowAllOutbound: true,
        })

        dbSecurityGroup.addIngressRule(ec2.Peer.anyIpv4(), ec2.Port.tcp(5432), 'Allow internet to read / write to aurora')

        // Full spec https://github.com/aws/aws-cdk/issues/20197#issuecomment-1117555047
        const dbCluster = new rds.DatabaseCluster(this, 'DbCluster', {
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
                    subnetType: ec2.SubnetType.PUBLIC,
                }),
            },
            port: 5432,
        })

        Aspects.of(dbCluster).add({
            visit(node) {
                if (node instanceof CfnDBCluster) {
                    node.serverlessV2ScalingConfiguration = {
                        minCapacity: 0.5,
                        maxCapacity: 1,
                    }
                }
            },
        });


        // TODO: lambda must be in the VPC?
        // TODO: lambda must be able to access the Aurora Port?
    }
}