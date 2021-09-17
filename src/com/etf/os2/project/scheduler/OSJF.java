package com.etf.os2.project.scheduler;

import java.util.LinkedList;

import com.etf.os2.project.process.Pcb;
import com.etf.os2.project.process.PcbData;

public class OSJF extends Scheduler {
	private double alfa;
	private boolean isPreemptive;
	private LinkedList<Pcb> lists[];
	private static final double STARTING_TAU = 200; // STA STAVITI ZA POCETNU VREDNOST??

	private LinkedList<Pcb> antiStarvingList = new LinkedList<>();
	// private static final long WAITING_LIMIT = 600000;

	private static int processor = 0;
	private boolean firstPut = true;

	@Override
	public Pcb get(int cpuId) {

		/*
		 * if (antiStarvingList.isEmpty()) return null;
		 */

		Pcb ret = null;

		/*
		 * if ((Pcb.getCurrentTime() - antiStarvingList.getFirst().getPcbData().putTime)
		 * >= OSJF.WAITING_LIMIT) { ret = antiStarvingList.removeFirst();
		 * lists[ret.getAffinity()].remove(ret); } else {
		 */
		int i = 0;

		while (i < Pcb.RUNNING.length) {

			if (!(lists[cpuId].isEmpty())) {
				ret = lists[cpuId].removeFirst();
				break;
			}

			cpuId = (cpuId + 1) % Pcb.RUNNING.length;
			i++;
		}
		/*
		 * antiStarvingList.remove(ret); }
		 */

		if (this.isPreemptive && ret != null)
			ret.getPcbData().executionStartTime = Pcb.getCurrentTime();
		// ovo iznad je dobro ako proces koji se vrati ovom funkcijom postane running
		// odmah

		return ret;

	}

	@Override
	public void put(Pcb pcb) {
		if (firstPut) {
			lists = new LinkedList[Pcb.RUNNING.length];
			for (int i = 0; i < Pcb.RUNNING.length; i++)
				lists[i] = new LinkedList<>();
			firstPut = false;
		}

		if (pcb.getPcbData() == null) {
			pcb.setPcbData(new PcbData());
			pcb.getPcbData().tau = OSJF.STARTING_TAU;

			// samo prvi put se postavlja affinity da bi se ravnomerno opteretili procesori
			pcb.setAffinity(processor);
			processor = (processor + 1) % Pcb.RUNNING.length;

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

		for (Pcb cur : lists[pcb.getAffinity()]) {
			if (cur.getPcbData().tau > pcb.getPcbData().tau)
				break;
			i++;
		}

		lists[pcb.getAffinity()].add(i, pcb);

		pcb.getPcbData().putTime = Pcb.getCurrentTime();

		/*
		 * antiStarvingList.addLast(pcb);
		 */

		if (this.isPreemptive) {
			
			Pcb pom = Pcb.RUNNING[pcb.getAffinity()];
			if (pom != Pcb.IDLE) {
				double timeLeft = pom.getPcbData().tau + pom.getPcbData().executionStartTime - Pcb.getCurrentTime();
				if (timeLeft > pcb.getPcbData().tau && timeLeft > 0) {
					pom.getPcbData().isPreempted = true;
					pom.preempt();
					return;
				}
			}
			
			double maxTimeLeft = 0;
			Pcb toPreempt = null;
			for (Pcb cur : Pcb.RUNNING) {
				if (cur != Pcb.IDLE && cur != pom) {
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

	public OSJF(double alfa, boolean isPreemptive) throws GNeispravnaVrednostAlfa {
		if (alfa < 0 || alfa > 1)
			throw new GNeispravnaVrednostAlfa();
		this.alfa = alfa;
		this.isPreemptive = isPreemptive;
	}

}
