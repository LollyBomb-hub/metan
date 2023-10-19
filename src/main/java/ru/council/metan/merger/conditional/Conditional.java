package ru.council.metan.merger.conditional;

import lombok.*;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "conditional")
@Getter
@Setter
@ToString
public class Conditional {

    @XmlElement(name = "on")
    private String on;

    @XmlElement(name = "parent")
    private String parent;

    @XmlElement(name = "current")
    private String current;

    @XmlElement
    private Then then;

    @XmlElement
    private Either either;

}
