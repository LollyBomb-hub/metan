package ru.council.metan.merger;

import lombok.*;
import ru.council.metan.merger.actions.Append;
import ru.council.metan.merger.actions.Put;
import ru.council.metan.merger.actions.Remove;
import ru.council.metan.merger.conditional.Conditional;

import javax.xml.bind.annotation.*;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "each")
@Getter
@Setter
@ToString
public class Each {

    @XmlAttribute
    private Boolean parent = false;

    @XmlAttribute
    private Boolean current = false;

    @XmlElement(name = "control-flow")
    private ControlFlow controlFlow;

    @XmlElement(name = "conditional")
    private Conditional conditional;

    @XmlElement
    private Put put;

    @XmlElement
    private Append append;

    @XmlElement(name = "remove")
    private List<Remove> removeList;

}
