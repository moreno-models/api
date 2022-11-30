import * as cdk from 'aws-cdk-lib';
import { Construct } from 'constructs';
import * as lambda from 'aws-cdk-lib/aws-lambda';
import {Code} from "aws-cdk-lib/aws-lambda";

export class ServiceStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    new lambda.Function(this,'MorenoModelsService', {
      runtime: lambda.Runtime.JAVA_11,
      handler: 'net.stepniak.morenomodels.serviceserverless.Handler',
      code: Code.fromAsset('../build/libs/serverless-api-0.0.1-SNAPSHOT-all.jar')
    });
  }
}
