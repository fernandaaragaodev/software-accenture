import { apiRequest } from "./api";

export interface ColleagueContext {
  name: string;
  professionalProfile: string;
  profileLabel: string;
  typicalStartTime: string;
}

export interface WorkplaceContext {
  employeeRegistered: boolean;
  displayName: string;
  teamId: number | null;
  teamName: string | null;
  teamPreferredFloor: string | null;
  professionalProfile: string;
  profileLabel: string;
  visibleColleagues: ColleagueContext[];
}

export interface RoomSuggestion {
  roomId: number;
  name: string;
  floor: string;
  score: number;
  freeDesksInInterval: number;
  scoreReasons: string[];
}

export async function fetchWorkplaceContext(
  userName: string,
): Promise<WorkplaceContext> {
  const params = new URLSearchParams({ userName });
  return apiRequest<WorkplaceContext>(
    `/workspace/context?${params.toString()}`,
  );
}

export interface TeamMember {
  id: number;
  displayName: string;
  professionalProfile: string;
  profileLabel: string;
  hidePresenceFromTeam: boolean;
}

export interface ManagerTeam {
  id: number;
  name: string;
  preferredFloor: string;
  members: TeamMember[];
}

export async function fetchManagerTeams(
  requesterName: string,
  requesterRole: string,
): Promise<ManagerTeam[]> {
  const params = new URLSearchParams({ requesterName, requesterRole });
  return apiRequest<ManagerTeam[]>(
    `/workspace/manager-teams?${params.toString()}`,
  );
}

export async function fetchRoomSuggestions(params: {
  userName: string;
  date: string;
  start: string;
  end: string;
  limit?: number;
}): Promise<RoomSuggestion[]> {
  const q = new URLSearchParams({
    userName: params.userName,
    date: params.date,
    start: params.start,
    end: params.end,
    limit: String(params.limit ?? 5),
  });
  return apiRequest<RoomSuggestion[]>(
    `/workspace/room-suggestions?${q.toString()}`,
  );
}
