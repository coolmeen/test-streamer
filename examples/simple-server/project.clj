(defproject net.unit8.test-streamer/simple-server "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [net.unit8/test-streamer-server "0.1.0-SNAPSHOT"]
                 [org.apache.poi/poi "3.10-FINAL"]
                 [org.apache.poi/poi-ooxml "3.10-FINAL"]
                 [commons-io/commons-io "2.4"]
                 [org.apache.commons/commons-lang3 "3.2.1"]
                 [org.easymock/easymock "3.2"]]
  :source-paths ["src/clj"]
  :java-source-paths ["src/java"]
  :jvm-opts ["-Dsun.zip.disableMemoryMapping"]
  :javac-options ["-target" "1.6" "-source" "1.6" "-Xlint:-options"]
  :main test-streamer.examples.simple-server)
