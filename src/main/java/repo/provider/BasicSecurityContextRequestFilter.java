package repo.provider;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.security.Principal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class BasicSecurityContextRequestFilter implements ContainerRequestFilter {

    private static final String BASIC = "Basic";
    private final Map<String, User> userMap = new HashMap<>();

    public void add(User user) {
        userMap.put(user.authentication, user);
    }

    public void addAll(Collection<User> users) {
        for(User user: users) {
            add(user);
        }
    }

    @Override
    public void filter(ContainerRequestContext containerRequest) throws WebApplicationException {

        final String authorization = containerRequest.getHeaderString(HttpHeaders.AUTHORIZATION);

        if(authorization != null && authorization.startsWith(BASIC)) {
            final User user = userMap.get(authorization.substring(BASIC.length() + 1));
            final boolean secure = containerRequest.getUriInfo().getBaseUri().getScheme().equals("https");
            containerRequest.setSecurityContext(new BasicSecurityContext(user, secure));
        }
    }

    private class BasicSecurityContext implements SecurityContext {
        private final User user;
        private final boolean secure;

        BasicSecurityContext(User user, boolean secure) {
            this.user = user;
            this.secure = secure;
        }

        @Override
        public Principal getUserPrincipal() {
            return user.principal;
        }

        @Override
        public boolean isUserInRole(String role) {
            return user.roles.contains(role);
        }

        @Override
        public boolean isSecure() {
            return secure;
        }

        @Override
        public String getAuthenticationScheme() {
            return BASIC_AUTH;
        }
    }
}