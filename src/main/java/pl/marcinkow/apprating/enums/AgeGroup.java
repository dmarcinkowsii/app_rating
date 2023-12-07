package pl.marcinkow.apprating.enums;

public enum AgeGroup {
    AGE_GROUP_1(null, 19),
    AGE_GROUP_2(20, 26),
    AGE_GROUP_3(27, 35),
    AGE_GROUP_4(36, 45),
    AGE_GROUP_5(46, 55),
    AGE_GROUP_6(56, 65),
    AGE_GROUP_7(66, null);

    private final Integer minAge;
    private final Integer maxAge;

    AgeGroup(Integer minAge, Integer maxAge) {
        this.minAge = minAge;
        this.maxAge = maxAge;
    }

    public Integer getMinAge() {
        return minAge;
    }

    public Integer getMaxAge() {
        return maxAge;
    }
}
