package com.audition.integration;

import com.audition.common.exception.SystemException;
import com.audition.common.logging.AuditionLogger;
import com.audition.model.AuditionPost;
import java.net.URI;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class AuditionIntegrationClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditionIntegrationClient.class);
    private static final String BASE_URL = "https://jsonplaceholder.typicode.com/posts";
    private final AuditionLogger auditionLogger;

    @Autowired
    private RestTemplate restTemplate;

    public AuditionIntegrationClient(AuditionLogger auditionLogger) {
        this.auditionLogger = auditionLogger;
    }

    public List<AuditionPost> getPosts() {
        // make RestTemplate call to get Posts from https://jsonplaceholder.typicode.com/posts
        try {
            AuditionPost[] posts = restTemplate.getForObject(BASE_URL, AuditionPost[].class);
            return posts == null ? List.of() : List.of(posts);
        } catch (final HttpClientErrorException e) {
            auditionLogger.logErrorWithException(LOGGER,
                String.format("Error occurred while fetching posts: %s", e.getMessage()), e);
            throw new SystemException(String.format("Error occurred while fetching posts: %s", e.getMessage()),
                "Integration Error", e.getStatusCode().value());
        }
    }

    public AuditionPost getPostById(final int id) {
        // get post by post ID call from https://jsonplaceholder.typicode.com/posts/
        try {
            URI url = UriComponentsBuilder.fromHttpUrl(BASE_URL + "/{id}").buildAndExpand(id).encode().toUri();
            AuditionPost post = restTemplate.getForObject(url, AuditionPost.class);
            return post == null ? new AuditionPost() : post;
        } catch (final HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                auditionLogger.logErrorWithException(LOGGER,
                    String.format("Post with id %d not found: %s", id, e.getMessage()), e);
                throw new SystemException("Cannot find a Post with id " + id, "Resource Not Found",
                    404);
            } else {
                // Find a better way to handle the exception so that the original error message is not lost. Feel free to change this function.
                auditionLogger.logErrorWithException(LOGGER,
                    String.format("Error occurred while fetching post with id %d: %s", id, e.getMessage()), e);
                throw new SystemException(
                    String.format("Error occurred while fetching post with id %d: %s", id, e.getMessage()),
                    "Integration Error", e.getStatusCode().value());
            }
        }
    }

    // TODO Write a method GET comments for a post from https://jsonplaceholder.typicode.com/posts/{postId}/comments - the comments must be returned as part of the post.

    // TODO write a method. GET comments for a particular Post from https://jsonplaceholder.typicode.com/comments?postId={postId}.
    // The comments are a separate list that needs to be returned to the API consumers. Hint: this is not part of the AuditionPost pojo.
}
