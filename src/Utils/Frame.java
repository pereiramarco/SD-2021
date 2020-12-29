package Utils;

import java.util.List;

public class Frame {
    public final Tag tag; //identifica o que está dentro da frame (a que interação se refere)
    public final List<byte[]> data; //list das várias infos a enviar de forma agnóstica

    public Frame(Tag tagG, List<byte[]> dataG) {
        tag=tagG;
        data=dataG;
    }
}
