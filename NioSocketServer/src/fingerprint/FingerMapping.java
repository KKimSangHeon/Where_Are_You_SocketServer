package fingerprint;

import java.util.ArrayList;

import com.example.myapplication.socketdata.AverageRSSI;

/**
 * Created by SangHeon on 2016-05-03.
 * 핑거프린팅 결과를 매핑하기 위한 클래스
 */
public class FingerMapping {
  
    public int[] mappingWithFingerPrinting(ArrayList<AverageRSSI> Beacon,double[][] average_table,double[][] var_table) {
        int[] arr=getCoordinate(Beacon, average_table, var_table, 1);     //ArrayList,평균테이블,분산테이블,KNN알고리즘에서의 K 를 매개변수로 넘긴다.   
        return arr;
    }

    public int[] getCoordinate(ArrayList<AverageRSSI> Beacon, double[][] average_Arr, double[][] var_Arr, int amount) {
        double[][] coordinate = new double[amount][2];
        int[] result_coordinate=new int[2];
        double rssi, gap;
        int column_index = 0, minor, row_index = 1;
        int[] received_beaconArr = new int[average_Arr[0].length];
        double[][] small_gap_index_list_Arr = new double[amount][2];  //0열에는 gap의 최소값의 행 인덱스 ,1열에는 gap값
        initSmallGapIndexArr(small_gap_index_list_Arr);
        while (row_index < average_Arr.length) {
            gap=0;
            for (int i = 0; i < Beacon.size(); i++)  //인식된 비콘에 대한 처리
            {
                minor = Beacon.get(i).minor;
                rssi = Beacon.get(i).getAverageRSSI();
                column_index = getMinorIndex(average_Arr, minor, received_beaconArr);

                gap += getGap(average_Arr, var_Arr, rssi, row_index, column_index);
            }

            for (int i = 2; i < received_beaconArr.length; i++)              //인식되지 않은 비콘에 대한 처리
            {
                if (received_beaconArr[i] == 0) {
                    column_index = getMinorIndex(average_Arr, i, received_beaconArr);
                    gap += getGap(average_Arr, var_Arr, -9999, row_index, column_index);  //비콘이 인식되지 않았으므로 rssi값은 존재하지 않음
                }
            }

            setSmallGapIndexArr(small_gap_index_list_Arr, row_index, gap);  //row인덱스와 gap을 삽입가능한지 판단하고 삽입한다.

            row_index++;
        }

        for(int i=0;i<small_gap_index_list_Arr.length;i++)  //좌표값을 coordinate 배열에 저장 (K가 2일경우 2쌍의 좌표가 2차원 배열에 저장된다)
        {
            row_index=(int)small_gap_index_list_Arr[i][0];
            coordinate[i][0]=average_Arr[row_index][0];
            coordinate[i][1]=average_Arr[row_index][1];
        }

        result_coordinate= applyKNNAlgorithm(coordinate);   //KNN 알고리즘 적용

        return result_coordinate;
    }

    public int[] applyKNNAlgorithm(double[][] coordinate_arr){
        double coordinate_x=0;
        double coordinate_y=0;
        int[] coordinate=new int[2];
        for(int i=0;i<coordinate_arr.length;i++){
            coordinate_x+=coordinate_arr[i][0];
            coordinate_y+=coordinate_arr[i][1];
        }
        coordinate_x/=coordinate_arr.length;
        coordinate_y/=coordinate_arr.length;

        coordinate[0]=(int)coordinate_x;
        coordinate[1]=(int)coordinate_y;

        return coordinate;
    }

    public void initSmallGapIndexArr(double[][] small_gap_index_list_Arr) {  //모든 배열의 1열(gap가 저장되는)을 큰 수로 초기화
        for (int i = 0; i < small_gap_index_list_Arr.length; i++) {
            small_gap_index_list_Arr[i][1] = 1000000;
        }
    }

    public void setSmallGapIndexArr(double[][] small_gap_index_list_Arr, int row_index, double gap) {
        int size_of_index_arr;
        size_of_index_arr = small_gap_index_list_Arr.length;


        if (small_gap_index_list_Arr[size_of_index_arr - 1][1] > gap)    //마지막 요소보다 작으면 마지막 요소에 삽입
        {
            small_gap_index_list_Arr[size_of_index_arr - 1][0] = row_index;
            small_gap_index_list_Arr[size_of_index_arr - 1][1] = gap;
        }

        double temp_row_index;
        double temp_gap;
        for (int i = 0; i < size_of_index_arr - 1; i++) {       //배열을 정렬
            for (int j = 0; j < size_of_index_arr - 1 - i; j++) {
                if (small_gap_index_list_Arr[j][1] > small_gap_index_list_Arr[j + 1][1]) {
                    temp_row_index = small_gap_index_list_Arr[j][0];
                    temp_gap = small_gap_index_list_Arr[j][1];
                    small_gap_index_list_Arr[j][0] = small_gap_index_list_Arr[j + 1][0];
                    small_gap_index_list_Arr[j][1] = small_gap_index_list_Arr[j + 1][1];
                    small_gap_index_list_Arr[j + 1][0] = temp_row_index;
                    small_gap_index_list_Arr[j + 1][1] = temp_gap;
                }
            }
        }
    }

    public int getMinorIndex(double[][] arr, double minor, int[] received_beaconArr) {
        double temp_minor;

        if (minor < 1000) {
            temp_minor = 16692 - 2 + minor;
            for (int j = 2; j < arr[0].length; j++) {
                if (temp_minor == arr[0][j])   //Minor와 일치하는 열 인덱스를 찾는다.
                {
                    return j;
                }
            }
        }

        for (int j = 2; j < arr[0].length; j++) {
            if (minor == arr[0][j])   //읽힌 비콘Minor와 일치하는 열 인덱스를 찾는다.
            {
                received_beaconArr[j] = 1;
                return j;
            }
        }

        return -9999;
    }

    public double getGap(double[][] average_Arr, double var_Arr[][], double rssi, int row_index, int column_index) {
        double gap;
        double divisor = Math.sqrt(Math.sqrt(var_Arr[row_index][column_index]));

        if (rssi != -9999) {   //비콘이 인식되었을 경우 gap을 구하는 영역
            if (average_Arr[row_index][column_index] != -9999) //인식된 비콘이 테이블에 존재할 경우(-9999가 아닌경우) 테이블에 존재하는 값과 해당비콘의 값의 차이를 구하고 제수를 나눈다.
            {
            	gap=Math.abs(average_Arr[row_index][column_index] + (-1 * rssi));
            	
            	if(gap>=20)
            		gap=10000;        	          	
            	else
            		gap = Math.abs((average_Arr[row_index][column_index] + (-1 * rssi)) / divisor);
            	
               
            } else if (rssi >= -85)  //인식된게 85이상인데 table 상에서 존재하지 않으면(-9999인경우) 그것은 값을 배제하기 위해 무한대의 값을 줌
            {
                gap = 10000;
            } else    //비콘을 인식을 하였는데 테이블에 존재하지 않을 경우(-9999인경우) 테이블의 값을 -200으로 주고 차이를 구함
            {
                gap = Math.abs((average_Arr[row_index][column_index] + 200));
            }
        } else {   //비콘이 인식되지 않았을 경우 gap을 구하는 영역
            if (average_Arr[row_index][column_index] != -9999)
                gap = Math.abs((average_Arr[row_index][column_index] + (-1 * -200)) / divisor);
            else
                gap = 0;

        }
        return gap;
    }
}



