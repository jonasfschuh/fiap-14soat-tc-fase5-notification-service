package br.com.fiap.infrastructure.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;

@Configuration
public class AwsSqsConfiguration {

    @Value("${aws.region:us-east-1}")
    private String region;

    @Value("${aws.access-key-id:test}")
    private String accessKeyId;

    @Value("${aws.secret-access-key:test}")
    private String secretAccessKey;

    @Value("${aws.endpoint-override:}")
    private String endpointOverride;

    @Bean
    @ConditionalOnProperty(name = "aws.sqs.enabled", havingValue = "true", matchIfMissing = false)
    public SqsClient sqsClient() {
        var builder = SqsClient.builder().region(Region.of(region));

        if (StringUtils.hasText(endpointOverride)) {
            builder.credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKeyId, secretAccessKey)
            ));
            builder.endpointOverride(URI.create(endpointOverride));
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }

        return builder.build();
    }
}
