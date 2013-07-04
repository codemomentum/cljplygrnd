package org.codemomentum.cljplygrnd;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.HashMap;
import java.util.Map;

public class WordCountBolt extends BaseBasicBolt {

    Map<String, Integer> counts = new HashMap<String, Integer>();


    @Override
    public void execute(Tuple tuple, BasicOutputCollector collector) {
        String word = tuple.getString(0);
        Integer count = counts.get(word);
        if (count == null) count = 0;
        count++;
        counts.put(word, count);
        collector.emit(new Values(word, count));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("word", "count"));
    }

    @Override
    public void cleanup() {
        System.out.println("***************");
        for(Map.Entry<String,Integer> entry:counts.entrySet()) {
            System.out.println(entry.getKey()+"+"+entry.getValue());
        }
        System.out.println("***************");
    }
}
