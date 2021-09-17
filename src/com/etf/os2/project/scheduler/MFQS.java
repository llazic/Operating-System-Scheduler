package com.etf.os2.project.scheduler;

import java.util.LinkedList;

import com.etf.os2.project.process.Pcb;
import com.etf.os2.project.process.PcbData;

public class MFQS extends Scheduler {

	private LinkedList<Pcb>[] lists; // manji broj je veci prioritet
	private long[] timeSlices;

	// maksimalno cekanje u jednom redu; moze da se zameni nizom waiting limita: za svaki red poseban limit
	private static final long WAITING_LIMIT = 3000;

	@Override
	public Pcb get(int cpuId) {

		// sprecavanje izgladnjivanja
		for (int i = 1; i < lists.length; i++) {
			while (!(lists[i].isEmpty())) {
				// posmatra se samo prvi jer se Pcb-ovi redom ubacuju u redove
				// pa ako prvi nije probio limit, nisu ni ostali
				if (Pcb.getCurrentTime() - lists[i].getFirst().getPcbData().putTime >= MFQS.WAITING_LIMIT) {
					Pcb p = lists[i].removeFirst();
					p.getPcbData().putTime = Pcb.getCurrentTime();
					lists[i - 1].addLast(p);
				} else
					break;
			}
		}

		// odabir sledeceg za izvrsavanje
		for (int i = 0; i < lists.length; i++) {
			if (!(lists[i].isEmpty())) {
				Pcb ret = lists[i].removeFirst();
				return ret;
			}
		}

		return null;
	}

	@Override
	public void put(Pcb pcb) {

		if (pcb.getPcbData() == null) {

			pcb.setPcbData(new PcbData());
			pcb.getPcbData().previousPriority = (pcb.getPriority() > lists.length - 1) ? lists.length - 1 : pcb.getPriority();
			// u slucaju da je prioritet veci od predvidjenog

		} else {

			if (pcb.getPreviousState() == Pcb.ProcessState.BLOCKED) {
				if (pcb.getPcbData().previousPriority > 0) {
					pcb.getPcbData().previousPriority--;
				}
			} else if (pcb.getPcbData().previousPriority < lists.length - 1) {
				pcb.getPcbData().previousPriority++;
			}

		}

		// ovde je previousPriority zapravo trenutni prioritet
		pcb.setTimeslice(timeSlices[pcb.getPcbData().previousPriority]);
		lists[pcb.getPcbData().previousPriority].addLast(pcb);
	}

	public MFQS(long[] timeSlices) {
		this.timeSlices = timeSlices;
		lists = new LinkedList[timeSlices.length];
		for (int i = 0; i < lists.length; i++)
			lists[i] = new LinkedList<>();
	}

}
