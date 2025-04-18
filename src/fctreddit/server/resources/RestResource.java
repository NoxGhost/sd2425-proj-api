package fctreddit.server.resources;


import fctreddit.api.java.Result;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;


public class RestResource {

    /**
     * Given a Result<T>, either returns the value, or throws the JAX-WS Exception
     * matching the error code...
     */
    protected <T> T fromJavaResult(Result<T> result) {
        if (result.isOK())
            return result.value();
        else
            throw new WebApplicationException(statusCodeFrom(result));
    }

    /**
     * Translates a Result<T> to a HTTP Status code
     */
    private static Status statusCodeFrom(Result<?> result) {
        switch (result.error()) {
            case CONFLICT:
                return Status.CONFLICT;
            case NOT_FOUND:
                return Status.NOT_FOUND;
            case FORBIDDEN:
                return Status.FORBIDDEN;
            case TIMEOUT:
            case BAD_REQUEST:
                return Status.BAD_REQUEST;
            case NOT_IMPLEMENTED:
                return Status.NOT_IMPLEMENTED;
            case INTERNAL_ERROR:
                return Status.INTERNAL_SERVER_ERROR;
            case OK:
                return result.value() == null ? Status.NO_CONTENT : Status.OK;
            default:
                return Status.INTERNAL_SERVER_ERROR;
        }
    }

}
