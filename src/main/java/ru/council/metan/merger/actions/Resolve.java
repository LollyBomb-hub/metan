package ru.council.metan.merger.actions;

import lombok.*;
import ru.council.metan.merger.conditional.Then;

import javax.xml.bind.annotation.*;

@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "resolve")
@Getter
@Setter
@ToString
public class Resolve {

    @XmlElement(name = "parent")
    private String parent;

    @XmlElement(name = "current")
    private String current;

    @XmlElement
    private Then then;

}
