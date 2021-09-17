package com.etf.os2.project.scheduler;

import java.util.LinkedList;

import com.etf.os2.project.process.Pcb;
import com.etf.os2.project.process.PcbData;

public class SJF extends Scheduler {
	private double alfa;
	private boolean isPreemptive;
	private LinkedList<Pcb> list = new LinkedList<>();
	private static final double STARTING_TAU = 200; // STA STAVITI ZA POCETNU VREDNOST??

	private LinkedList<Pcb> antiStarvingList = new LinkedList<>();
	private static final long WAITING_LIMIT = 600000;

	@Override
	public Pcb get(int cpuId) {
		if (list.isEmpty())
			return null;

		Pcb ret = null;

		if ((Pcb.getCurrentTime() - antiStarvingList.getFirst().getPcbData().putTime) >= SJF.WAITING_LIMIT) {
			ret = antiStarvingList.removeFirst();
			list.remove(ret);
		} else {
			ret = list.removeFirst();
			antiStarvingList.remove(ret);
		}

		if (this.isPreemptive)
			ret.getPcbData().executionStartTime = Pcb.getCurrentTime();
		// ovo iznad je dobro ako proces koji se vrati ovom funkcijom postane running
		// odmah

		return ret;

	}

	@Override
	public void put(Pcb pcb) {

		if (pcb.getPcbData() == null) {
			pcb.setPcbData(new PcbData());
			pcb.getPcbData().tau = SJF.STARTING_TAU;
		} else if (this.isPreemptive && pcb.getPcbData().isPreempted) {

			if (pcb.getPcbData().tau - pcb.getExecutionTime() <= 0)
				pcb.getPcbData().tau = alfa * pcb.getExecutionTime() + (1 - alfa) * pcb.getPcbData().tau;
			else
				pcb.getPcbData().tau -= pcb.getExecutionTime();

			pcb.getPcbData().isPreempted = false;
		} else {
			pcb.getPcbData().tau = alfa * pcb.getExecutionTime() + (1 - alfa) * pcb.getPcbData().tau;
		}

		int i = 0;

		for (Pcb cur : list) {
			if (cur.getPcbData().tau > pcb.getPcbData().tau)
				break;
			i++;
		}

		list.add(i, pcb);

		pcb.getPcbData().putTime = Pcb.getCurrentTime();
		antiStarvingList.addLast(pcb);

		if (this.isPreemptive) {
			double maxTimeLeft = 0;
			Pcb toPreempt = null;
			for (Pcb cur : Pcb.RUNNING) {
				if (cur != Pcb.IDLE) {
					double timeLeft = cur.getPcbData().tau + cur.getPcbData().executionStartTime - Pcb.getCurrentTime();
					if (timeLeft > pcb.getPcbData().tau && timeLeft > maxTimeLeft) {
						maxTimeLeft = timeLeft;
						toPreempt = cur;
					}
				}
			}

			if (toPreempt != null) {
				toPreempt.getPcbData().isPreempted = true;
				toPreempt.preempt();
			}
		}
	}

	public SJF(double alfa, boolean isPreemptive) throws GNeispravnaVrednostAlfa {
		if (alfa < 0 || alfa > 1)
			throw new GNeispravnaVrednostAlfa();
		this.alfa = alfa;
		this.isPreemptive = isPreemptive;
	}

}
