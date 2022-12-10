import * as cdk from 'aws-cdk-lib';
import { Construct } from 'constructs';
import * as lambda from 'aws-cdk-lib/aws-lambda';
import * as apigw from "aws-cdk-lib/aws-apigateway";
import * as iam from 'aws-cdk-lib/aws-iam';
import {integrateApiWithLambda} from "./openapi-integration";

export class ServiceStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    const lambdaService = new lambda.Function(this,'MorenoModelsService', {
      functionName: 'MorenoModelsHandler',
      runtime: lambda.Runtime.JAVA_11,
      handler: 'io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest',
      code: lambda.Code.fromAsset('../build/function.zip'),
      memorySize: 512,
    });

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
