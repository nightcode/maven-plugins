/*
 * Copyright (C) The NightCode Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.nightcode.maven.plugin.gzip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;

/**
 * Simple maven gzip plugin compresses every webapp directory files individually.
 */
@Mojo(name = "gzip", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class GzipPlugin extends AbstractMojo {

  private static final String[] EMPTY_STRING_ARRAY = new String[] {};
  private static final String[] DEFAULT_INCLUDES = new String[]{"**/*.css", "**/*.js"};

  /**
   * Single directory for extra files to include in the WAR.
   */
  @Parameter(property = "warSourceDirector", defaultValue = "${basedir}/src/main/webapp")
  private File warSourceDirectory;

  /**
   * The directory where the webapp is built.
   */
  @Parameter(property = "webappDirectory",
             defaultValue = "${project.build.directory}/${project.build.finalName}")
  private File webappDirectory;

  /**
   * The comma separated list of tokens to include when gzip the content of the warSourceDirectory.
   */
  @Parameter(property = "excludes")
  private List<String> excludes;

  /**
   * The comma separated list of tokens to include when gzip the content of the warSourceDirectory.
   */
  @Parameter(property = "includes")
  private List<String> includes;

  /**
   * Skip execution.
   */
  @Parameter(property = "skip")
  private boolean skip;

  public GzipPlugin() {
  }

  @Override public void execute() throws MojoExecutionException, MojoFailureException {
    if (skip) {
      getLog().debug("maven-gzip-plugin skipped");
      return;
    }

    if ((warSourceDirectory == null) || (!warSourceDirectory.exists())) {
      return;
    }

    DirectoryScanner scanner = new DirectoryScanner();
    scanner.setBasedir(warSourceDirectory);

    if (includes == null) {
      scanner.setIncludes(DEFAULT_INCLUDES);
    } else {
      scanner.setIncludes(includes.toArray(EMPTY_STRING_ARRAY));
    }

    if ((excludes != null) && !excludes.isEmpty()) {
      scanner.setExcludes(excludes.toArray(EMPTY_STRING_ARRAY));
    }
    scanner.addDefaultExcludes();
    scanner.scan();
    try {
      for (String name : scanner.getIncludedFiles()) {
        File srcFile = new File(warSourceDirectory, name);
        File dstFile = new File(webappDirectory, name);
        if (!dstFile.getParentFile().exists() && !dstFile.getParentFile().mkdirs()) {
          throw new MojoExecutionException("could not create resource output directory: "
              + dstFile.getParentFile());
        }
        FileUtils.forceDelete(dstFile);
        gzip(srcFile, dstFile);
      }
    } catch (IOException ex) {
      throw new MojoExecutionException("could not perform gzip", ex);
    }
  }

  private void gzip(File src, File dst) throws IOException {
    if (src == null || !src.exists()) {
      return;
    }
    if (".gz".equalsIgnoreCase(FileUtils.getExtension(src.getName()))) {
      return;
    }
    File gzipped = new File(dst.getAbsolutePath() + ".gz");
    FileInputStream in = null;
    GZIPOutputStream out = null;
    try {
      in = new FileInputStream(src);
      out = new GZIPOutputStream(new FileOutputStream(gzipped));
      IOUtil.copy(in, out);
    } finally {
      IOUtil.close(in);
      IOUtil.close(out);
    }
    getLog().info(String.format("gzip: %s -> %s [%db, compression %d%%]", src.getName()
        , gzipped.getName(), gzipped.length()
        , (100 - gzipped.length() * 100 / Math.max(1, src.length()))));
  }
}
