package ru.council.metan.merger;

import lombok.*;
import ru.council.metan.merger.actions.Append;
import ru.council.metan.merger.actions.Put;
import ru.council.metan.merger.actions.Remove;
import ru.council.metan.merger.actions.Resolve;

import javax.xml.bind.annotation.*;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "merger")
@Getter
@Setter
@ToString
public class Merger {

    @XmlAttribute(name = "supplyParent")
    private Boolean supplyParent = false;

    @XmlElement
    private Resolve resolve;

    @XmlElement
    private Each each;

    @XmlElement
    private Put put;

    @XmlElement
    private Append append;

    @XmlElement(name = "remove")
    private List<Remove> removeList;

}
