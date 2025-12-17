package wethinkcode.schedule.transfer;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SlotDO
{
    private LocalTime start;
    private LocalTime end;

    public SlotDO(){
    }

    @JsonCreator
    public SlotDO(
            @JsonProperty( value = "start" ) LocalTime from,
            @JsonProperty( value = "end" ) LocalTime to ){
        start = from;
        end = to;
    }

    public LocalTime getStart(){
        return start;
    }

    public LocalTime getEnd(){
        return end;
    }
}