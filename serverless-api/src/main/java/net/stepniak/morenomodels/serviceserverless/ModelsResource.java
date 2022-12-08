package net.stepniak.morenomodels.serviceserverless;

import net.stepniak.morenomodels.serviceserverless.generated.model.Models;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/models")
public class ModelsResource {
    @GET
    @Produces({"application/json"})
    public Response listModels(@QueryParam("nextToken") @Size(max = 512) String nextToken,
                               @QueryParam("pageSize") @Min(1) @Max(1000) Integer pageSize,
                               @QueryParam("showArchived") Boolean showArchived,
                               @QueryParam("givenName") @Size(max = 64) String givenName
    ) {
        // TODO: verify https://github.com/quarkiverse/quarkus-openapi-generato
        // TODO: verify other generators
        // TODO: verify deployment
        return Response.ok(new Models()).build();
    }

}
