# Maven plugins

Simple but useful maven plugins.

`gzip-maven-plugin`
Compresses every webapp directory files individually.
```
  <project>
  ...
    <build>
      <plugins>
  ...
        <plugin>
          <groupId>org.nightcode.maven.plugins</groupId>
          <artifactId>gzip-maven-plugin</artifactId>
          <configuration>
            <includes>
              <include>**/*.js</include>
              <include>**/*.css</include>
            </includes>
            <excludes>
              <exclude>**/*.xml</exclude>
            </excludes>
          </configuration>
          <executions>
            <execution>
              <goals>
                <goal>gzip</goal>
              </goals>
            </execution>
          </executions>
        </plugin>
  ...
      </plugins>
    </build>
  ...
  </project>
```

Feedback is welcome. Please don't hesitate to open up a new [github issue](https://github.com/nightcode/maven-plugins/issues) or simply drop me a line at <dmitry@nightcode.org>.
