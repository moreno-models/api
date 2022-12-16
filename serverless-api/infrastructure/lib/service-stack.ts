import * as cdk from 'aws-cdk-lib';
import {Construct} from 'constructs';
import * as lambda from 'aws-cdk-lib/aws-lambda';
import * as apigw from "aws-cdk-lib/aws-apigateway";
import * as iam from 'aws-cdk-lib/aws-iam';
import {integrateApiWithLambda} from "./openapi-integration";
import {Duration} from "aws-cdk-lib";
import {PolicyStatement} from "aws-cdk-lib/aws-iam";
import * as s3 from "aws-cdk-lib/aws-s3";
import * as rds from 'aws-cdk-lib/aws-rds';


export interface ServiceStackProps extends cdk.StackProps {
    photoBucket: s3.Bucket
    auroraCluster: rds.DatabaseCluster,
}

export class ServiceStack extends cdk.Stack {
    constructor(scope: Construct, id: string, props: ServiceStackProps) {
        super(scope, id, props);

        const lambdaService = new lambda.Function(this, 'MorenoModelsService', {
            functionName: 'MorenoModelsHandler',
            runtime: lambda.Runtime.JAVA_11,
            handler: 'io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest',
            code: lambda.Code.fromAsset('../build/function.zip'),
            memorySize: 1024,
            timeout: Duration.minutes(2),
            environment: {
                QUARKUS_DATASOURCE_CREDENTIALS_PROVIDER: 'aws-secrets-manager',
                QUARKUS_DATASOURCE_JDBC_URL: `jdbc:postgresql://${props.auroraCluster.clusterEndpoint.socketAddress}/morenomodels`,
                AWS_SECRETS_MANAGER_SECRET_ARN: props.auroraCluster.secret!.secretArn!,
                BUCKET_NAME: props.photoBucket.bucketName,
            }
        });


        lambdaService.addToRolePolicy(new PolicyStatement({
            resources: [props.auroraCluster.secret?.secretArn!],
            actions: ['secretsmanager:GetSecretValue']
        }));

        props.photoBucket.grantReadWrite(lambdaService.grantPrincipal);
        props.photoBucket.grantDelete(lambdaService.grantPrincipal);

        const api = new apigw.SpecRestApi(this, 'MorenoAPISpecification', {
            restApiName: 'MorenoModels',
            apiDefinition: new apigw.InlineApiDefinition(
                integrateApiWithLambda(
                    '../../model/src/main/resources/moreno-models.yaml',
                    {
                        region: this.region,
                        partition: this.partition,
                        functionArn: lambdaService.functionArn
                    }
                )
            ),
        });

        // Allow the Lambda to be called by any Rest API
        lambdaService.addPermission('APIGWPermission', {
            principal: new iam.ServicePrincipal('apigateway.amazonaws.com'),
            sourceArn: `${api.arnForExecuteApi('*', '/*', '*')}`
        })
    }
}
