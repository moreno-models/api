import {integrateApiWithLambda} from "../lib/openapi-integration";


test('Adds x-amazon-api-gateway-integration', () => {
    const json = integrateApiWithLambda('../../model/src/main/resources/moreno-models.yaml', {
        region: 'us-east-2',
        functionArn: 'arn:aws:lambda:us-east-2:123456789012:function:my-function',
        partition: 'aws'
    });

    const expectedIntegration = {
        uri: 'arn:aws:apigateway:us-east-2:lambda:path/2015-03-31/functions/arn:aws:lambda:us-east-2:123456789012:function:my-function/invocations',
        httpMethod: "POST",
        type: "aws_proxy",
    };

    expect(json['paths']['/models']['get']['x-amazon-apigateway-integration'])
        .toEqual(expectedIntegration)
    expect(json['paths']['/models/{modelSlug}']['put']['x-amazon-apigateway-integration'])
        .toEqual(expectedIntegration)
    expect(json['paths']['/models/{modelSlug}']['delete']['x-amazon-apigateway-integration'])
        .toEqual(expectedIntegration)
    expect(json['paths']['/models']['post']['x-amazon-apigateway-integration'])
        .toEqual(expectedIntegration)
})