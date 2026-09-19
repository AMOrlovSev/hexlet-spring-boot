package io.hexlet.spring_boot;

import io.hexlet.spring_boot.model.Post;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SpringBootApplication
@RestController
public class Application {

    private List<Post> posts = new ArrayList<>();

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @GetMapping("/")
    public String home() {
        return "Hello World!";
    }

    @GetMapping("/about")
    public String about() {
        return "This is simple Spring blog!";
    }

    @GetMapping("/posts")
    public ResponseEntity<List<Post>> index(@RequestParam(defaultValue = "10") Integer limit) {
        List<Post> postsResult = posts.stream().limit(limit).toList();

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(posts.size()))
                .body(postsResult);
    }

    @PostMapping("/posts")
    public ResponseEntity<Post> create(@RequestBody Post data) {
        Post post = new Post();
        post.setAuthor(data.getAuthor());
        post.setTitle(data.getTitle());
        post.setContent(data.getContent());
        post.setCreatedAt(LocalDateTime.now());

        posts.add(post);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(post.getTitle())
                .toUri();
        return ResponseEntity.created(location).body(post);
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<Post> show(@PathVariable String id) {
        var post = posts.stream().filter(p -> p.getTitle().equals(id)).findFirst();
        return ResponseEntity.of(post);
    }

    @PutMapping("/posts/{id}")
    public ResponseEntity<Post> update(@PathVariable String id, @RequestBody Post data) {
        var post = posts.stream()
                .filter(p -> p.getTitle().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        post.setAuthor(data.getAuthor());
        post.setTitle(data.getTitle());
        post.setContent(data.getContent());
        post.setCreatedAt(LocalDateTime.now());

        return ResponseEntity.ok(post);
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> destroy(@PathVariable String id) {
        boolean removed = posts.removeIf(p -> p.getTitle().equals(id));
        return removed
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

}
