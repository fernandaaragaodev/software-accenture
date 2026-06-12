import type { ReactNode } from 'react';

export interface WizardStep {
  id: string;
  label: string;
}

interface WizardProps {
  steps: WizardStep[];
  currentStep: number;
  children: ReactNode;
}

export function Wizard({ steps, currentStep, children }: WizardProps) {
  return (
    <div className="wizard">
      <ol className="wizard-steps">
        {steps.map((step, index) => {
          const state =
            index < currentStep ? 'completed' : index === currentStep ? 'active' : 'pending';
          return (
            <li key={step.id} className={`wizard-step wizard-step-${state}`}>
              <span className="wizard-step-number">{index + 1}</span>
              <span className="wizard-step-label">{step.label}</span>
            </li>
          );
        })}
      </ol>
      <div className="wizard-content">{children}</div>
    </div>
  );
}
