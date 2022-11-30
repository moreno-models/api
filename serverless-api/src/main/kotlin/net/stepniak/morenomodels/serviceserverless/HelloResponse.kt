package net.stepniak.morenomodels.serviceserverless

data class HelloResponse(val message: String, val input: Map<String, Any>) : Response()
