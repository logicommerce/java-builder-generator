# Java Builder Generator

Automatically generate builder classes for your Java objects. This library is intended for internal use at LogiCommerce.

## Demo

Given these classes:
```java
@GenerateBuilder
public class BlogPost {
	// ...
	public void setAuthor(BlogPostAuthor author) { /* ... */ }
	public void setTags(Map<Integer, BlogPostTag> tags) { /* ... */ }
}

@GenerateBuilder
public class BlogPostAuthor {
	// ...
	public void setId(int id) { /* ... */ }
	public void setAliases(List<String> aliases) { /* ... */ }
}

@GenerateBuilder
public class BlogPostTag {
	// ...
	public void setName(String name) { /* ... */ }
	public void setValue(String value) { /* ... */ }
}
```

The generator creates several linked builders that can be used as:
```java
BlogPost blogPost = new BlogPostBuilder<>()
    .author()
        .id(123)
        .addAlias("the_author")
        .addAlias("another_alias")
    .done()
    .addTag(1)
        .name("date")
        .value("yesterday")
    .done()
    .addTag(2)
        .name("likes")
        .value("350")
    .done()
    .build();
```

The code above is fully type-safe and it is equivalent to:
```java
BlogPostAuthor author = new BlogPostAuthor();
author.setAliases(List.of("the_author", "another_alias"));

BlogPostTag tag1 = new BlogPostTag();
tag1.setName("date");
tag1.setValue("yesterday");

BlogPostTag tag2 = new BlogPostTag();
tag2.setName("likes");
tag2.setValue("350");

BlogPost post = new BlogPost();
post.setAuthor(author);
post.setTags(Map.ofEntries(
    Map.entry(1, tag1),
    Map.entry(2, tag2)
));
```

## Setup and Usage

1. All modules containing classes for which builders should be generated (using `@GenerateBuilder`) need to add the `annotations` dependency:
```xml
<dependency>
  <groupId>com.logicommerce</groupId>
  <artifactId>builder-generator-annotations</artifactId>
  <version>...</version>
</dependency>
```

2. The code generation can be performed as a test or an integration step. In either case, all relevant classes (all classes for which builders will be generated, and all interceptors) need to be available in the classpath.
Add the `codegen` dependency to the corresponding module. For example, if the generation will be performed as a test (notice the `test` scope):
```xml
<dependency>
  <groupId>com.logicommerce</groupId>
  <artifactId>builder-generator-codegen</artifactId>
  <version>...</version>
  <scope>test</scope>
</dependency>
```

3. Call the code generator from a test or an integration step.

```java
import com.logicommerce.buildergenerator.codegen.BuilderGeneratorMain;
import com.logicommerce.buildergenerator.codegen.BuilderGeneratorConfig;

void generateBuilders() throws IOException {
    // Create the config object with 1 or more generation tasks
    var config = new BuilderGeneratorConfig();
    config.addGenerationTask(
        // List of locations to scan for @GenerateBuilder
        List.of("/path/to/src/main/java/com/myorg/models", "/path/to/src/main/java/com/myorg/dtos"),
        // Output location where generated builders will be created.
        // In this example, builders will be used for tests (notice the "src/test")
        "/path/to/src/test/java/com/myorg/generatedbuilders",
        // Package with interceptors to hook into the object creation process (optional)
        "/path/to/src/test/java/com/myorg/builderinterceptors"
    );
    new BuilderGeneratorMain(config).generate();
}
```

4. To generate a builder for a class, annotate it with `@GenerateBuilder`. Make sure it has public setters, they will be used to create the builder.

```java
import com.logicommerce.buildergenerator.annotations.GenerateBuilder;

// Builder name defaults to "MyClassBuilder".
// It can be overriden with @GenerateBuilder(name = "CustomBuilderName")
@GenerateBuilder
public class MyClass {
    public void setName(String name) { /* ... */ }
    public void setAge(int age) { /* ... */ }
}
```

> [!IMPORTANT]
> Do not add `@GenerateBuilder` to abstract classes, add it to each concrete child you want to be able to build.

> [!NOTE]
> If your setter receives another object (or collection of objects) and you want to use nested/chained builders, make sure the other object is also annotated with `@GenerateBuilder`.

### Using interceptors

You can customize the build process for a given class by creating an interceptor inside the interceptors package (specified in the configuration).

All classes implementing `BuilderInterceptor<T>` inside the package will be automatically picked up and used.

For example, to hook into the creation of class `com.myorg.models.Basket`:

```java
package com.myorg.builderinterceptors;

import com.logicommerce.buildergenerator.annotations.BuilderInterceptor;

import java.util.UUID;
import com.myorg.models.Basket;

public class BasketBuilderInterceptor implements BuilderInterceptor<Basket> {

	@Override
	public void beforeBuild(Basket basket) {
		basket.setId(UUID.randomUUID().toString());
	}

	@Override
	public void afterBuild(Basket basket) {
		basket.updateShippingInformation();
	}

}
```

> [!TIP]
> Unlike `@GenerateBuilder`, interceptors are **not** limited to concrete classes. You can define an interceptor for an abstract class and it will be used/inherited by the builders of all its subclasses. Interceptors are always applied in order, from most abstract (superclass) to most concrete (subclass).


## Limitations

- The code generator must be executed in a Linux system.

## Development

### Deploy to maven

First, set up `m2/settings.xml` and GPG keys. Then, run: 

```sh
mvn clean deploy -P deploy
```
