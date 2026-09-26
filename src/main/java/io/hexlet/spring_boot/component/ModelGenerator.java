package io.hexlet.spring_boot.component;

import io.hexlet.spring_boot.model.Post;
import io.hexlet.spring_boot.model.Tag;
import io.hexlet.spring_boot.model.User;
import io.hexlet.spring_boot.repository.TagRepository;
import io.hexlet.spring_boot.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ModelGenerator {

    private final Faker faker;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;

    public ModelGenerator(Faker faker,
                          UserRepository userRepository,
                          TagRepository tagRepository) {
        this.faker = faker;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
    }

    @PostConstruct
    public void generateData() {
        // 1. Сначала теги (иначе на них не будет ссылок)
        var tagNames = List.of("spring", "java", "kotlin", "jpa", "rest", "hexlet");
        var tags = tagNames.stream()
                .map(name -> {
                    var tag = new Tag();
                    tag.setName(name);
                    return tagRepository.save(tag);
                })
                .toList();

        // 2. Пользователи с постами и случайными тегами
        for (int i = 0; i < 5; i++) {
            var user = new User();
            user.setFirstName(faker.name().firstName());
            user.setLastName(faker.name().lastName());
            user.setEmail(faker.internet().emailAddress());
            user.setBirthday(faker.timeAndDate().birthday());

            var post = new Post();
            post.setTitle(faker.book().title());
            post.setContent(faker.lorem().paragraph());
            post.setPublished(faker.bool().bool());
            post.setAuthor(user);

            // добавляем 1–2 случайных тега
            int count = 1 + faker.random().nextInt(2);
            for (int j = 0; j < count; j++) {
                int idx = faker.random().nextInt(tags.size());
                post.addTag(tags.get(idx));
            }

            user.addPost(post);
            userRepository.save(user);
        }
    }
}