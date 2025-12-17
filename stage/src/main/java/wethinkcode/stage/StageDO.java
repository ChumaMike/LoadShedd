package wethinkcode.stage;

/**
 * I am a data/transfer object for communicating the loadshedding stage.
 */
public class StageDO
{
    private int stage;

    /**
     * Default constructor is needed otherwise the JSON mapper
     * can't create an instance.
     */
    public StageDO(){
        stage = 0;
    }

    public StageDO( int s ){
        stage = s;
    }

    public int getStage(){
        return stage;
    }

    // Added setter so Jackson can deserialize JSON {"stage": X} into this object
    public void setStage( int s ){
        this.stage = s;
    }
}