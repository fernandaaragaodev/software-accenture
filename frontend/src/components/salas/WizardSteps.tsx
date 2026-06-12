interface WizardStepsProps {
  steps: string[];
  current: number;
}

export function WizardSteps({ steps, current }: WizardStepsProps) {
  return (
    <nav className="wizard-steps" aria-label="Etapas do cadastro">
      {steps.map((label, index) => {
        const stepNum = index + 1;
        const isActive = stepNum === current;
        const isDone = stepNum < current;
        return (
          <div
            key={label}
            className={`wizard-step ${isActive ? 'active' : ''} ${isDone ? 'done' : ''}`}
            aria-current={isActive ? 'step' : undefined}
          >
            <span className="wizard-step-number">{isDone ? '✓' : stepNum}</span>
            <span className="wizard-step-label">{label}</span>
          </div>
        );
      })}
    </nav>
  );
}
