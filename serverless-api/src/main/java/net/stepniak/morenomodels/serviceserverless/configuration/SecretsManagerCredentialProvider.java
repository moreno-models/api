package net.stepniak.morenomodels.serviceserverless.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.arc.Unremovable;
import io.quarkus.credentials.CredentialsProvider;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
@Unremovable
@Named("aws-secrets-manager")
public class SecretsManagerCredentialProvider implements CredentialsProvider {
    private static final Logger LOG = Logger.getLogger(SecretsManagerCredentialProvider.class);
    private static final String PROVIDER_NAME = "aws-secrets-manager";
    @Inject
    SecretsManagerClient secretsManagerClient;

    @Inject
    ObjectMapper objectMapper;

    @ConfigProperty(name = "aws-secrets-manager.secret-arn")
    Optional<String> secretArn;

    @Override
    public Map<String, String> getCredentials(String credentialsProviderName) {
        long startMillis = System.currentTimeMillis();
        LOG.info("Getting credentials...");
        if (!credentialsProviderName.equals(PROVIDER_NAME)) {
            return null;
        }
        if (secretArn.isEmpty()) {
            throw new RuntimeException("Tried to use SecretsManager Credential Provider "
                    + " without providing the secret ARN.");
        }

        GetSecretValueResponse secretValueResponse = secretsManagerClient
                .getSecretValue(GetSecretValueRequest.builder()
                        .secretId(secretArn.get())
                        .build()
                );
        try {
            SecretModel secret = objectMapper.readValue(secretValueResponse.secretString(), SecretModel.class);

            LOG.info("Got credentials in: [" + (System.currentTimeMillis() - startMillis) + "])");
            return Map.of(
                    USER_PROPERTY_NAME, secret.getUsername(),
                    PASSWORD_PROPERTY_NAME, secret.getPassword()
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    private static class SecretModel {
        private final String username;
        private final String password;

        private SecretModel(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }
        public String getPassword() {
            return password;
        }
    }
}
