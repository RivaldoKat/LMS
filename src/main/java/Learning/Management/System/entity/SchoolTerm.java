package Learning.Management.System.entity;

import lombok.Getter;

@Getter
public enum SchoolTerm {
    Term1(1),
    Term2(2),
    Term3(3),
    Term4(4);

    private final int terms;

    SchoolTerm(int term) {
        this.terms = term;
    }
}