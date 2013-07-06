package org.codemomentum.cljplygrnd.first;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;

public class WordCountTopology {
    public static void main(String[] args) throws Exception {

        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("RandomSentenceSpout", new RandomSentenceSpout(), 5);

        builder.setBolt("SplitSentenceBolt", new SplitSentenceBolt(), 8)
                .shuffleGrouping("RandomSentenceSpout");
        builder.setBolt("WordCountBolt", new WordCountBolt(), 12)
                .fieldsGrouping("SplitSentenceBolt", new Fields("word"));

        Config conf = new Config();
        conf.setDebug(true);


        conf.setMaxTaskParallelism(3);

        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("word-count", conf, builder.createTopology());

        Thread.sleep(10000);

        cluster.shutdown();
    }
}
