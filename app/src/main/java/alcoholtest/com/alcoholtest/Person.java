package alcoholtest.com.alcoholtest;

public class Person {
    private String name;
    private boolean isMale;
    private double age;
    private double weight;
    private double height;
    private double created;

    public Person(String name, boolean isMale, double age, double weight, double height, long created) {
        this.name = name;
        this.isMale = isMale;
        this.age = age;
        this.weight = weight;
        this.height = height;
        this.created = created;
    }

    public String getName() {
        return name;
    }

    public boolean isMale() {
        return isMale;
    }

    public double getAge() {
        return age;
    }

    public double getWeight() {
        return weight;
    }

    public double getHeight() {
        return height;
    }

    public double getCreated() {
        return created;
    }

}
