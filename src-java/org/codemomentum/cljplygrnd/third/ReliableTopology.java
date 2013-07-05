package org.codemomentum.cljplygrnd.third;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class ReliableTopology {

    public static class RandomFailureBolt extends BaseRichBolt {

        private static final Integer MAX_PERCENT_FAIL = 10;
        Random random = new Random();
        private OutputCollector collector;

        public void execute(Tuple input) {
            Integer r = random.nextInt(100);
            if (r > MAX_PERCENT_FAIL) {
                collector.ack(input);
            } else {
                collector.fail(input);
            }
        }

        public void prepare(Map stormConf, TopologyContext context,
                            OutputCollector collector) {
            this.collector = collector;
        }

        public void declareOutputFields(OutputFieldsDeclarer declarer) {
        }

    }

    public static class ReliableSpout extends BaseRichSpout {

        Random rand;

        private SpoutOutputCollector collector;

        private Map<String, Object> undelivered = new HashMap<String, Object>();


        @Override
        public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
            outputFieldsDeclarer.declare(new Fields("word"));
        }

        @Override
        public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
            collector = spoutOutputCollector;
            rand = new Random();
        }

        @Override
        public void nextTuple() {
            Utils.sleep(100);
            String[] sentences = new String[]{
                    "a little brown dog",
                    "the man petted the dog",
                    "four score and seven years ago",
                    "an apple a day keeps the doctor away"};
            String sentence = sentences[rand.nextInt(sentences.length)];
            String uuid = UUID.randomUUID().toString();
            undelivered.put(uuid, sentence);
            collector.emit(new Values(sentence), uuid);
        }

        @Override
        public void ack(Object msgId) {
            undelivered.remove(msgId.toString());
        }

        @Override
        public void fail(Object msgId) {
            System.out.println("There is an undelivered message! :" + msgId);
            throw new RuntimeException("There is an undelivered message! :" + msgId);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("spout", new ReliableSpout());
        builder.setBolt("bolt", new RandomFailureBolt()).
                fieldsGrouping("spout", new Fields("word"));

        LocalCluster cluster = new LocalCluster();
        Config conf = new Config();
        conf.setDebug(true);
        cluster.submitTopology("test", conf, builder.createTopology());
        while (true) {
            Thread.sleep(1000);
        }
    }
}
