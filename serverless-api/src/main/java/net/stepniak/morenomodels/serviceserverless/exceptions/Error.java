package net.stepniak.morenomodels.serviceserverless.exceptions;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Error {
    Integer statusCode;
    String message;
}
