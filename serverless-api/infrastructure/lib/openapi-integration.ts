import * as fs from "fs";
import * as yaml from "yaml";

export interface Metadata {
    region: string,
    partition: string,
    functionArn: string,
}


export function integrateApiWithLambda(openApiPath: string, metadata: Metadata) {
    const openApiDefinition = fs.readFileSync(openApiPath, 'utf8');
    const parsedYaml = yaml.parse(openApiDefinition);

    for (const key in parsedYaml['paths']) {
        addXApiIntegration(parsedYaml['paths'][key], metadata);
    }

    return parsedYaml;
}

export function addXApiIntegration(object: any, metadata: Metadata) {
    const operations = ['get', 'put', 'delete', 'post'];
    for (const key in object) {
        if (operations.includes(key)) {
            object[key]['x-amazon-apigateway-integration'] = {
                uri: `arn:${metadata.partition}:apigateway:${metadata.region}:lambda:path/2015-03-31/functions/${metadata.functionArn}/invocations`,
                httpMethod: 'POST',
                type: 'aws_proxy'
            }
        }
    }
}