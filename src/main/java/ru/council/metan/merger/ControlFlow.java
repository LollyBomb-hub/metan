package ru.council.metan.merger;

import lombok.*;
import ru.council.metan.merger.conditional.Conditional;
import ru.council.metan.merger.conditional.Either;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "control-flow")
@Getter
@Setter
@ToString
public class ControlFlow {

    @XmlElement
    private Each each;

    @XmlElement
    private Conditional conditional;

    @XmlElement
    private Either either;

}
