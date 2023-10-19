package ru.council.metan.merger.actions;

import lombok.*;

import javax.xml.bind.annotation.*;

@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "synthesis")
@Getter
@Setter
@ToString
public class Synthesis {

    @XmlAttribute(name = "on")
    private String on;

}
