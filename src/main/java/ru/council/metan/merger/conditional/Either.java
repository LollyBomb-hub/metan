package ru.council.metan.merger.conditional;

import lombok.*;
import ru.council.metan.merger.Each;
import ru.council.metan.merger.actions.Append;
import ru.council.metan.merger.actions.Put;
import ru.council.metan.merger.actions.Remove;
import ru.council.metan.merger.actions.Resolve;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "then")
@Getter
@Setter
@ToString
public class Either {

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
