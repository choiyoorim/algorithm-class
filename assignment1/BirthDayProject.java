import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;

public class BirthDayProject {
    public static void main(String[] args) throws IOException {
        long beforeTime = System.currentTimeMillis();
        ArrayList<Student> students = new ArrayList<Student>();
        // 파일 입력 -> 학생 정보 students ArrayList에 저장
        BufferedReader br = new BufferedReader(new FileReader("./birthday.in"));
        FileWriter fw = new FileWriter("./birthday.out");
        FileWriter fw1 = new FileWriter("./birthday1.out");
        String str;

        while ((str = br.readLine()) != null) {
            StringTokenizer st = new StringTokenizer(str);
            String name = st.nextToken();
            String birthdate = st.nextToken();
            String birthmonth = birthdate.substring(0,2);
            String birthday = birthdate.substring(2);
            students.add(new Student(name,birthmonth,birthday));
        }

//        int count = 0;
//        for(int i = 0; i<students.size(); i++){
//            for(int j = i+1; j<students.size(); j++){
//                if(Integer.parseInt(students.get(i).birthMonth) == Integer.parseInt(students.get(j).birthMonth)
//                && Integer.parseInt(students.get(i).birthday) == Integer.parseInt(students.get(j).birthday)){
//                    count++;
//                }
//            }
//        }
//        System.out.println(count);

        ArrayList<Student> mergedList = MergeSort(students);

        long afterTime = System.currentTimeMillis();
        long secDiffTime = (afterTime - beforeTime); //두 시간에 차 계산
        System.out.println("시간차이(m) : "+secDiffTime);

        int count1 = 0;
        for(int i = 0; i<mergedList.size(); i++){
            int targetBirth = Integer.parseInt(mergedList.get(i).birthMonth + mergedList.get(i).birthday);
            int low = i;
            int high = mergedList.size() - 1;
            int mid;

            while(low <= high) {
                mid = (low + high) / 2;
                int midBirth = Integer.parseInt(mergedList.get(mid).birthMonth + mergedList.get(mid).birthday);
                if (midBirth == targetBirth){
                    count1++;
                    break;
                }
                else if (midBirth > targetBirth)
                    high = mid - 1;
                else
                    low = mid + 1;
            }
        }
        System.out.println(count1);

        for(Student student:mergedList){
            fw.write(student.name + " " + student.birthMonth + student.birthday + "\n");
        }

        quicksort(students);
        for(Student student:students){
            fw1.write(student.name + " " + student.birthMonth + student.birthday + "\n");
        }

        br.close();
        fw.close();
        fw1.close();
    }

    private static ArrayList<Student> MergeSort(ArrayList<Student> array) {
        if(array.size()<=1) return array;

        ArrayList<Student> left = new ArrayList<>();
        ArrayList<Student> right= new ArrayList<>();
        int mid = array.size()/2;  //중간인덱스 찾기

        for(int i = 0; i<mid; i++){
            left.add(array.get(i));
        }
        for(int i = mid; i<array.size(); i++){
            right.add(array.get(i));
        }

        left = MergeSort(left);
        right = MergeSort(right);

        return merge(left,right);
    }

    private static ArrayList<Student> merge(ArrayList<Student> left, ArrayList<Student> right){
        ArrayList<Student> mergeList = new ArrayList<>();
        int i = 0, j = 0;

        while (i<left.size() && j<right.size()){
            int leftNum = Integer.parseInt(left.get(i).birthMonth);
            int rightNum = Integer.parseInt(right.get(j).birthMonth);
            if(leftNum <= rightNum){
                mergeList.add(left.get(i));
                i++;
            }
            else{
                mergeList.add(right.get(j));
                j++;
            }
        }

        while(i<left.size()){
            mergeList.add(left.get(i));
            i++;
        }

        while(j<right.size()){
            mergeList.add(right.get(j));
            j++;
        }

        return mergeList;
    }

    private static void quicksort(ArrayList<Student> arr){
        quicksort(arr,0,arr.size()-1);
    }

    private static void quicksort(ArrayList<Student> arr, int start, int end){
        if(start >= end) return;

        int pivot = start;
        int lo = start + 1;
        int hi = end;

        while(lo <= hi){
            while(lo <= end && Integer.parseInt(arr.get(lo).birthMonth) <= Integer.parseInt(arr.get(pivot).birthMonth))
                lo++;
            while(hi > start && Integer.parseInt(arr.get(hi).birthMonth) >= Integer.parseInt(arr.get(pivot).birthMonth))
                hi--;
            if(lo <= hi) Collections.swap(arr, lo, hi); //
        }

        Collections.swap(arr, hi, pivot);
        quicksort(arr, start, hi-1);
        quicksort(arr, hi+1, end);

    }
}


class Student{
    String name, birthMonth, birthday;

    public Student(String name, String birthMonth,String birthday){
        this.name = name;
        this.birthMonth = birthMonth;
        this.birthday = birthday;
    }
}