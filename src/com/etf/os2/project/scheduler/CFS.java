package com.etf.os2.project.scheduler;

import java.util.LinkedList;

import com.etf.os2.project.process.Pcb;
import com.etf.os2.project.process.PcbData;

public class CFS extends Scheduler {

	private LinkedList<Pcb> list = new LinkedList<Pcb>();
	private static final long MIN_TIME_SLICE = 5;

	@Override
	public Pcb get(int cpuId) {
		
		if (list.isEmpty()) return null;
		
		Pcb ret = list.removeFirst();
		ret.getPcbData().totalWaitingTime += Pcb.getCurrentTime();
		
		long timeSlice = ret.getPcbData().totalWaitingTime/Pcb.getProcessCount();
		if (timeSlice < CFS.MIN_TIME_SLICE) timeSlice = CFS.MIN_TIME_SLICE;
		ret.setTimeslice(timeSlice);
		
		return ret;
	}

	@Override
	public void put(Pcb pcb) {

		if (pcb.getPcbData() == null) {
			pcb.setPcbData(new PcbData());
			
			pcb.getPcbData().totalExecutionTime = 0;
			pcb.getPcbData().totalWaitingTime = -Pcb.getCurrentTime();
		} else {
			
			if (pcb.getPreviousState() == Pcb.ProcessState.RUNNING) {
				pcb.getPcbData().totalExecutionTime += pcb.getExecutionTime();
				//pcb.getPcbData().totalWaitingTime -= Pcb.getCurrentTime();
			}else {
				pcb.getPcbData().totalExecutionTime = 0;
				//pcb.getPcbData().totalWaitingTime = -Pcb.getCurrentTime();
			}
			pcb.getPcbData().totalWaitingTime = -Pcb.getCurrentTime();
			// kada se u get() sabere totalWaitingTime sa tadasnjim Pcb.getCurrentTime()
			// dobice se vreme cekanja u Scheduleru za taj nalet izvrsavanja
			// ako je u pitanju ukupno vreme cekanja treba pcb.getPcbData().totalWaitingTime -= Pcb.getCurrentTime();
			// ako je u pitanju vreme cekanja u ovom naletu treba pcb.getPcbData().totalWaitingTime = -Pcb.getCurrentTime();
		}
		
		
		int i = 0;

		for (Pcb cur : list) {
			if (cur.getPcbData().totalExecutionTime >= pcb.getPcbData().totalExecutionTime)
				break;							//bitno da bude >= (a ne >) jer onda ne moze da dodje do izgladnjivanja
			i++;
		}

		list.add(i, pcb);
	}

}
