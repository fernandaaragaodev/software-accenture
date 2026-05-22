import type { BookingPosition } from "./roomsPremiumUtils";

export interface BatchTeamMember {
  id: number;
  name: string;
  role: string;
  selected: boolean;
}

export interface BatchPositionAssignment {
  positionId: string;
  position: BookingPosition;
  memberIndex: number;
  assignedTo: string;
}

export function effectiveBatchMembers(
  members: BatchTeamMember[],
): BatchTeamMember[] {
  return members.filter((m) => m.selected);
}

export function toggleBatchMemberSelection(
  members: BatchTeamMember[],
  index: number,
): BatchTeamMember[] {
  return members.map((m, i) =>
    i === index ? { ...m, selected: !m.selected } : m,
  );
}

export function toggleAllBatchMembers(
  members: BatchTeamMember[],
): BatchTeamMember[] {
  const allSelected = members.every((m) => m.selected);
  return members.map((m) => ({ ...m, selected: !allSelected }));
}

export function findNextUnassignedMemberIndex(
  teamSize: number,
  assignments: BatchPositionAssignment[],
): number {
  for (let i = 0; i < teamSize; i++) {
    if (!assignments.some((a) => a.memberIndex === i)) return i;
  }
  return 0;
}

export function toggleBatchPositionAssignment(
  pos: BookingPosition,
  assignments: BatchPositionAssignment[],
  currentMemberIndex: number,
  effectiveMembers: BatchTeamMember[],
): { assignments: BatchPositionAssignment[]; currentMemberIndex: number } {
  const teamSize = effectiveMembers.length;
  if (teamSize === 0) {
    return { assignments, currentMemberIndex };
  }

  const memberName = effectiveMembers[currentMemberIndex]?.name ?? "";
  const existingIdx = assignments.findIndex((a) => a.positionId === pos.id);

  if (existingIdx > -1) {
    const assignedMemberIndex = assignments[existingIdx].memberIndex;
    if (assignedMemberIndex === currentMemberIndex) {
      const next = assignments.filter((_, i) => i !== existingIdx);
      return {
        assignments: next,
        currentMemberIndex: findNextUnassignedMemberIndex(teamSize, next),
      };
    }
    const withoutCurrent = assignments.filter(
      (a) => a.memberIndex !== currentMemberIndex,
    );
    const updated = withoutCurrent.map((a) =>
      a.positionId === pos.id
        ? { ...a, memberIndex: currentMemberIndex, assignedTo: memberName }
        : a,
    );
    return {
      assignments: updated,
      currentMemberIndex: findNextUnassignedMemberIndex(teamSize, updated),
    };
  }

  const withoutCurrent = assignments.filter(
    (a) => a.memberIndex !== currentMemberIndex,
  );
  const nextAssignments: BatchPositionAssignment[] = [
    ...withoutCurrent,
    {
      positionId: pos.id,
      position: pos,
      memberIndex: currentMemberIndex,
      assignedTo: memberName,
    },
  ];
  return {
    assignments: nextAssignments,
    currentMemberIndex: findNextUnassignedMemberIndex(
      teamSize,
      nextAssignments,
    ),
  };
}
