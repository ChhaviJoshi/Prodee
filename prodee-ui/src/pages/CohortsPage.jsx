import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { ArrowLeft, Users, Trophy, PlusCircle, LogIn } from "lucide-react";
import { apiGet, apiPost } from "../utils/api";

export default function CohortsPage() {
  const navigate = useNavigate();
  const [myCohorts, setMyCohorts] = useState([]);
  const [selectedCohort, setSelectedCohort] = useState(null);
  const [leaderboard, setLeaderboard] = useState([]);
  const [createName, setCreateName] = useState("");
  const [joinCode, setJoinCode] = useState("");
  const [message, setMessage] = useState("");

  async function loadMyCohorts() {
    try {
      const list = await apiGet("/api/cohorts/mine");
      const cohorts = Array.isArray(list) ? list : [];
      setMyCohorts(cohorts);
      if (!selectedCohort && cohorts.length > 0) {
        setSelectedCohort(cohorts[0]);
      }
      if (
        selectedCohort &&
        !cohorts.some((cohort) => cohort.id === selectedCohort.id)
      ) {
        setSelectedCohort(cohorts[0] || null);
      }
    } catch {
      setMyCohorts([]);
      setSelectedCohort(null);
    }
  }

  async function loadLeaderboard(cohortId) {
    try {
      const board = await apiGet(`/api/cohorts/${cohortId}/leaderboard`);
      setLeaderboard(Array.isArray(board) ? board : []);
    } catch {
      setLeaderboard([]);
    }
  }

  useEffect(() => {
    loadMyCohorts().catch(() => {});
  }, []);

  useEffect(() => {
    if (!selectedCohort?.id) {
      setLeaderboard([]);
      return;
    }
    loadLeaderboard(selectedCohort.id).catch(() => {});
  }, [selectedCohort?.id]);

  const topWeeklyScore = useMemo(
    () => Math.max(1, ...leaderboard.map((member) => member.weeklyScore || 0)),
    [leaderboard],
  );

  async function createCohort(e) {
    e.preventDefault();
    try {
      const created = await apiPost("/api/cohorts", { name: createName });
      setMessage("Cohort created. Share the join code with your friends.");
      setCreateName("");
      await loadMyCohorts();
      setSelectedCohort(created);
    } catch (err) {
      setMessage(err.message || "Failed to create cohort");
    }
  }

  async function joinCohort(e) {
    e.preventDefault();
    try {
      await apiPost(`/api/cohorts/join/${joinCode.toUpperCase()}`, {});
      setMessage("Joined cohort successfully.");
      setJoinCode("");
      await loadMyCohorts();
    } catch (err) {
      setMessage(err.message || "Failed to join cohort");
    }
  }

  return (
    <div className="max-w-7xl mx-auto px-4 py-6">
      <div className="flex items-center gap-3 mb-6">
        <button
          onClick={() => navigate("/")}
          className="pixel-border-sm p-1.5 bg-retro-card hover:bg-retro-input"
        >
          <ArrowLeft size={14} />
        </button>
        <Users size={20} className="text-retro-accent2" />
        <h1 className="font-pixel text-sm text-retro-text">
          The Cohort System
        </h1>
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-[360px_1fr] gap-4">
        <section className="space-y-4">
          <form
            onSubmit={createCohort}
            className="pixel-border bg-retro-surface p-4 space-y-3"
          >
            <p className="font-pixel text-[8px] text-retro-muted flex items-center gap-1">
              <PlusCircle size={11} /> CREATE COHORT
            </p>
            <input
              className="pixel-input w-full"
              placeholder="Cohort name"
              value={createName}
              onChange={(e) => setCreateName(e.target.value)}
              required
            />
            <button type="submit" className="pixel-btn w-full">
              Create
            </button>
          </form>

          <form
            onSubmit={joinCohort}
            className="pixel-border bg-retro-surface p-4 space-y-3"
          >
            <p className="font-pixel text-[8px] text-retro-muted flex items-center gap-1">
              <LogIn size={11} /> JOIN WITH CODE
            </p>
            <input
              className="pixel-input w-full uppercase"
              placeholder="AB12CD34"
              value={joinCode}
              onChange={(e) => setJoinCode(e.target.value)}
              required
            />
            <button type="submit" className="pixel-btn w-full">
              Join
            </button>
          </form>

          {message && (
            <div className="pixel-border-sm bg-retro-card p-2 font-pixel text-[8px] text-retro-muted">
              {message}
            </div>
          )}

          <div className="pixel-border bg-retro-surface p-4">
            <p className="font-pixel text-[8px] text-retro-muted mb-3">
              MY COHORTS
            </p>
            {myCohorts.length === 0 ? (
              <p className="font-pixel text-[8px] text-retro-muted">
                You have not joined any cohort yet.
              </p>
            ) : (
              <div className="space-y-2">
                {myCohorts.map((cohort) => (
                  <button
                    key={cohort.id}
                    onClick={() => setSelectedCohort(cohort)}
                    className={`w-full text-left pixel-border-sm p-2 ${
                      selectedCohort?.id === cohort.id
                        ? "bg-retro-accent text-white"
                        : "bg-retro-card hover:bg-retro-input"
                    }`}
                  >
                    <p className="font-pixel text-[8px]">{cohort.name}</p>
                    <p className="font-pixel text-[7px] opacity-80">
                      Join code: {cohort.joinCode}
                    </p>
                  </button>
                ))}
              </div>
            )}
          </div>
        </section>

        <section className="pixel-border bg-retro-surface p-4">
          {!selectedCohort ? (
            <p className="font-pixel text-[8px] text-retro-muted">
              Select a cohort to view leaderboard and progress.
            </p>
          ) : (
            <>
              <div className="flex items-center justify-between mb-4 gap-3">
                <div>
                  <h2 className="font-pixel text-[10px] text-retro-text">
                    {selectedCohort.name} Leaderboard
                  </h2>
                  <p className="font-pixel text-[7px] text-retro-muted">
                    Daily + Weekly scores with first-place rewards tracking
                  </p>
                </div>
                <div className="pixel-border-sm bg-retro-card px-2 py-1">
                  <p className="font-pixel text-[7px] text-retro-muted">
                    Join: {selectedCohort.joinCode}
                  </p>
                </div>
              </div>

              <div className="space-y-2">
                {leaderboard.length === 0 ? (
                  <p className="font-pixel text-[8px] text-retro-muted">
                    No member progress yet. Complete tasks and habits to score.
                  </p>
                ) : (
                  leaderboard.map((member) => {
                    const progress = Math.round(
                      ((member.weeklyScore || 0) / topWeeklyScore) * 100,
                    );
                    return (
                      <div
                        key={member.userId}
                        className="pixel-border-sm bg-retro-card p-3"
                      >
                        <div className="flex items-center justify-between gap-2 mb-2">
                          <p className="font-pixel text-[9px] text-retro-text">
                            #{member.rank} {member.username}
                          </p>
                          <p className="font-pixel text-[7px] text-retro-muted">
                            LVL {member.level}
                          </p>
                        </div>

                        <div className="flex items-center gap-2 mb-2">
                          <Trophy size={12} className="text-retro-coin" />
                          <p className="font-pixel text-[7px] text-retro-muted">
                            Daily: {member.dailyScore} | Weekly:{" "}
                            {member.weeklyScore}
                          </p>
                        </div>

                        <div className="xp-bar-track mb-1">
                          <div
                            className="xp-bar-fill"
                            style={{ width: `${progress}%` }}
                          />
                        </div>
                        <p className="font-pixel text-[7px] text-retro-muted">
                          Task Progress: {progress}% of top weekly performer
                        </p>
                        <p className="font-pixel text-[7px] text-retro-accent mt-1">
                          1st-place finishes: {member.firstPlaceFinishes}
                        </p>
                      </div>
                    );
                  })
                )}
              </div>
            </>
          )}
        </section>
      </div>
    </div>
  );
}
