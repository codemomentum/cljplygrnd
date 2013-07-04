(defproject cljplygrnd "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
                  [org.clojure/clojure "1.5.1"]
                  [incanter "1.4.1"
                   :exclusions [net.sourceforge.jplasma/jplasma]]
                  [storm "0.8.2"]
                  [lamina "0.5.0-rc3"]
                  [twitter-api "0.7.4"]
                  [clojurewerkz/cassaforte "1.0.0-rc5"]
                  ]
  :java-source-paths ["src-java"]
  :jar-exclusions [#"log4j\.properties" #"backtype" #"trident"
                   #"META-INF" #"meta-inf" #"\.yaml"]
  :uberjar-exclusions [#"log4j\.properties" #"backtype" #"trident"
                       #"META-INF" #"meta-inf" #"\.yaml"]
  :aot :all )

