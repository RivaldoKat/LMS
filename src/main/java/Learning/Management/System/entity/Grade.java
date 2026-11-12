package Learning.Management.System.entity;

import lombok.Getter;

@Getter
public enum Grade {
    Eight(8),
    Nine(9),
    Ten(10),
    Eleven(11),
    Twelve(12);

    private final int grade;

    Grade(int grd) {
        this.grade = grd;
    }

}
