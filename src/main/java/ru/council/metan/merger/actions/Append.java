package ru.council.metan.merger.actions;

import lombok.*;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "append")
@Getter
@Setter
@ToString
public class Append {

    @XmlAttribute(name = "source")
    private String source;

    @XmlAttribute(name = "target")
    private String target;

}
