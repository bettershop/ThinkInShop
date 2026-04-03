package com.laiketui.common;

import java.util.*;
import java.util.stream.Collectors;

class Person
{
    private String name;
    private int    age;

    public Person(String name, int age)
    {
        this.name = name;
        this.age = age;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return "Person{name='" + name + "', age=" + age + "}";
    }
}

//@SpringBootTest
class LaikeCommonApplicationTests
{

    public static void main(String[] args)
    {
        List<Person> people = new ArrayList<>();
        people.add(new Person("Alice", 25));
        people.add(new Person("Bob", 30));
        people.add(new Person("Charlie", 35));
        people.add(new Person("David", 40));

        // 动态定义需要过滤的姓名
        Set<String> namesToFilter = new HashSet<>(Arrays.asList("Bob", "David"));

        List<Person> filteredPeople = people.stream()
                .filter(person -> namesToFilter.contains(person.getName()))
                .collect(Collectors.toList());

        System.out.println("过滤后的人员列表: " + filteredPeople);
    }
}
