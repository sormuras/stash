import static com.github.sormuras.bach.ProjectInfo.Externals.Name.*;

import com.github.sormuras.bach.ProjectInfo;
import com.github.sormuras.bach.ProjectInfo.External;
import com.github.sormuras.bach.ProjectInfo.Externals;
import com.github.sormuras.bach.ProjectInfo.Tools;
import com.github.sormuras.bach.ProjectInfo.Tweak;
import com.github.sormuras.bach.project.JavaStyle;

@ProjectInfo(
    name = "stash",
    version = "1-ea",
    format = JavaStyle.GOOGLE,
    compileModulesForJavaRelease = 16,
    includeSourceFilesIntoModules = true,
    tools = @Tools(skip = "jlink"),
    tweaks = {
      @Tweak(tool = "javac", option = "-encoding", value = "UTF-8"),
      @Tweak(tool = "javac", option = "-g"),
      @Tweak(tool = "javac", option = "-parameters"),
      @Tweak(tool = "javac", option = "-Xlint"),
      @Tweak(tool = "javac", option = "-Werror"),
      @Tweak(tool = "javadoc", option = "-encoding", value = "UTF-8"),
      @Tweak(tool = "javadoc", option = "-notimestamp"),
      @Tweak(tool = "javadoc", option = "-Xdoclint:-missing"),
      @Tweak(tool = "javadoc", option = "-Werror"),
    },
    requires = "org.junit.platform.console",
    lookupExternal =
        @External(
            module = "com.github.sormuras.beethoven",
            via =
                "https://github.com/sormuras/beethoven/releases/download/1-ea/com.github.sormuras.beethoven@1-ea.jar"),
    lookupExternals = @Externals(name = Externals.Name.JUNIT, version = "5.8.0-M1"))
module bach.info {
  requires com.github.sormuras.bach;
}
