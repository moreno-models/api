import * as cdk from "aws-cdk-lib";
import {Construct} from "constructs";
import * as s3 from 'aws-cdk-lib/aws-s3';


export class PhotoStorageStack extends cdk.Stack {
    public readonly photoBucket: s3.Bucket;

    constructor(scope: Construct, id: string, props?: cdk.StackProps) {
        super(scope, id, props);

        this.photoBucket = new s3.Bucket(this, 'PhotoStorageBucket', {});
    }
}