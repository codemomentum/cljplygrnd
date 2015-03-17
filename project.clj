(defproject cljplygrnd "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
                  [org.clojure/clojure "1.5.1"]
                  [org.clojure/core.async "0.1.222.0-83d0c2-alpha"]
                  [incanter "1.4.1"
                   :exclusions [net.sourceforge.jplasma/jplasma]]
                  [storm "0.8.2"]
                  [lamina "0.5.0-rc3"]
                  [twitter-api "0.7.5"]
                  [clojurewerkz/cassaforte "1.0.0-rc5"]
                  [com.esotericsoftware.kryo/kryo "2.20"]
                  ;[de.javakaffee/kryo-serializers "2.20"]

                  ;messaging
                  [com.novemberain/langohr "2.9.0"]
                  [clj-http "0.9.1" :exclusions [org.apache.httpcomponents/httpclient]]
                  [org.apache.httpcomponents/httpclient "4.3.3"]

                  [liberator "0.11.0"]
                  ]
  :main cljplygrnd.core
  :java-source-paths ["src-java"]
  :jar-exclusions [#"log4j\.properties" #"backtype" #"trident"
                   #"META-INF" #"meta-inf" #"\.yaml"]
  :uberjar-exclusions [#"log4j\.properties" #"backtype" #"trident"
                       #"META-INF" #"meta-inf" #"\.yaml"]
  :aot [cljplygrnd.core]

  )
