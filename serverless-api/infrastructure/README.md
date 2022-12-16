# Welcome to your CDK TypeScript project

This is a blank project for CDK development with TypeScript.

The `cdk.json` file tells the CDK Toolkit how to execute your app.

## Useful commands

* `npm run build`   compile typescript to js
* `npm run watch`   watch for changes and compile
* `npm run test`    perform the jest unit tests
* `cdk deploy`      deploy this stack to your default AWS account/region
* `cdk diff`        compare deployed stack with current state
* `cdk synth`       emits the synthesized CloudFormation template

* `npm run build && cdk synth`
* `sam local start-api -n environment.json -t ./cdk.out/ServiceStack.template.json --warm-containers=EAGER`
* `rm -rf cdk.out && npm run build && cdk synth && cdk deploy ServiceStack`